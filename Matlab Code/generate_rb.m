% Create a network with several levels of hierarchy
% Generated as presented in Ravasz and Barabasi (2003)
%
% Input
%   - r: recursion level
%
% Output
%   - adj   : adjacency matrix
%   - coords: gplot coordinates
%
% Author: Erwan Le Martelot
% Date: 17/12/10

function [adj,coords] = generate_rb(r)

    if r > 3
        error('r value %d too large\n',r);
    end

    n = power(5,r+1);
    %adj = zeros(n,n)

    adj = [
        0 1 1 1 1
        1 0 1 1 0
        1 1 0 0 1
        1 1 0 0 1
        1 0 1 1 0
        ];

    coords = [
        1 1
        0 0
        2 0
        0 2
        2 2
        ];
    rot = [cos(pi/4) -sin(pi/4); sin(pi/4) cos(pi/4)];

    for i=1:r
        z = zeros(length(adj),length(adj));
        adj = [
            adj, z ,z, z, z
            z, adj, z, z, z
            z, z, adj, z, z
            z, z, z, adj, z
            z, z, z, z, adj
            ];
        adj = link_submat(adj,1,length(adj));
        sh = [ones(length(coords),1),zeros(length(coords),1)];
        sv = [zeros(length(coords),1),ones(length(coords),1)];
        ss = ones(length(coords),1)*coords(1,:);
        rot_coords = (coords-ss)*rot+ss;
        coords = [
            rot_coords+(length(coords)/i)*sh+(length(coords)/i)*sv,
            rot_coords
            rot_coords+(2*length(coords)/i)*sh,
            rot_coords+(2*length(coords)/i)*sv,
            rot_coords+(2*length(coords)/i)*sh+(2*length(coords)/i)*sv
            ];
    end

end

function adj = link_submat(adj,f,s)
    if s == 5
        adj(f+1,1) = 1; adj(1,f+1) = 1;
        adj(f+2,1) = 1; adj(1,f+2) = 1;
        adj(f+3,1) = 1; adj(1,f+3) = 1;
        adj(f+4,1) = 1; adj(1,f+4) = 1;
    else
        s = s/5;
        for j=1:4
            adj = link_submat(adj,f+j*s,s);
        end
    end
end

